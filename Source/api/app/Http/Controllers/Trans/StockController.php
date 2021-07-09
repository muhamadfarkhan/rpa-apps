<?php

namespace App\Http\Controllers\Trans;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\TrHTonase;
use App\Models\TrProduction;
use App\Models\TrStock;
use App\Models\MItem;

class StockController extends Controller
{
     /**
     * Instantiate a new areaController instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('auth');
    }

    /**
     * Get all Item Stock.
     *
     * @return Response
     */
    public function all()
    {
        $tonase = TrHTonase::orderBy('processed_at','desc')->get();

        foreach($tonase as $row){
            if($row->production->count()){
                $data[] = $row;
                $row->production = $row->production();
                $row->price = number_format($row->price);
                $row->rpa = $row->rpa;
            }
        }
        
        return response()->json(['stocks' => $data ], 200);
         
    }

    /**
     * Get detail item Stock.
     *
     * @return Response
     */
    public function detail($id)
    {
        try {
            $tonase = TrHTonase::findOrFail($id);

            $detail = array();
            if(!empty(TrHTonase::findOrFail($id)->production)){
                $tonaseDetail = TrHTonase::findOrFail($id)->detail;
                $production = TrHTonase::findOrFail($id)->production;

                $tonase['sum_ekor'] = $tonaseDetail->sum('ekor');
                $tonase['sum_kilo'] = $tonaseDetail->sum('kilogram');
                
                foreach($production as $row){
                    $detail[] = $row;
                    $row->qty = $row->qty.' unit';
                    $row->item_name = (!empty($row->item)) ? $row->item->name : '';
                    $row->price = 'Harga Modal ' . number_format($row->capital_price) . ' ' . 
                                    'Harga Jual ' . number_format($row->sell_price);
                }
            }

            return response()->json(['tonase_header' => $tonase, 'productions' => $detail], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'Production not found! ' . $e, 'error' => $e], 404);
        }

    }

    /**
     * Get detail item Stock.
     *
     * @return Response
     */
    public function detailItem(Request $request)
    {
        $tonase_id = $request->input('tonase_id');
        $item_id = $request->input('item_id');
        
        try {
            $tonase = TrHTonase::findOrFail($tonase_id);

            $detail = array();
            if(!empty(TrHTonase::findOrFail($tonase_id)->production)){
                $tonaseDetail = TrHTonase::findOrFail($tonase_id)->detail;
                $production = TrHTonase::findOrFail($tonase_id)->production->where('item_id',$item_id)->first();
                $stock = TrHTonase::findOrFail($tonase_id)->stock->where('item_id',$item_id);

                $tonase['sum_ekor'] = $tonaseDetail->sum('ekor');
                $tonase['sum_kilo'] = $tonaseDetail->sum('kilogram');
                
                foreach($stock as $row){
                    $detail[] = $row;
                    $row->qty = $row->qty.' unit';
                    $row->item_name = (!empty($row->item)) ? $row->item->name : '';
                }
            }

            return response()->json(['tonase_header' => $tonase, 'production' => $production, 'stocks' => $detail], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'Production not found! ' . $e, 'error' => $e], 404);
        }

    }

    /**
     * Change a existing user.
     *
     * @param  Request  $request
     * @return Response
     */
    public function allocate(Request $request)
    {
        //validate incoming request 
        $this->validate($request, [
            'tonase_id' => 'required|integer',
            'item_id' => 'required|integer',
            'area_id' => 'required|integer',
            'seller_id' => 'required|integer',
            'unit' => 'required|integer',
        ]);

        $tonase_id = (int) $request->input('tonase_id');
        $item_id = (int) $request->input('item_id');
        $qty = (int) $request->input('unit');

        // return $this->checkLimitTotal($tonase_id,$item_id,$qty);

        if($this->checkLimitTotal($tonase_id,$item_id,$qty)){

            try {

                $stockItem = new TrStock;
                $stockItem->tonase_id = $tonase_id;
                $stockItem->item_id = $item_id;
                $stockItem->area_id = (int) $request->input('area_id');
                $stockItem->seller_id = (int) $request->input('seller_id');
                $stockItem->qty = $qty;
                
                $stockItem->save();

                //return successful response
                return response()->json(['stock_item' => $stockItem, 'message' => 'Successfully allocated'], 201);

            } catch (\Exception $e) {
                //return error message
                return response()->json(['message' => 'Allocate failed!', 'error' => $e], 409);
            }

        }else{
            
            return response()->json(['message' => 'Allocate failed!', 'error' => 'Alokasi melebihi stok yang tersedia'], 409);
        }

    }

    private function checkLimitTotal($tonase_id,$item_id,$qty){

        $totalStockProd = TrProduction::where('item_id',$item_id)
                        ->where('tonase_id',$tonase_id)->first();

        $totalLastStock = TrStock::where('item_id',$item_id)
        ->where('tonase_id',$tonase_id)->sum('qty');

        // return $totalLastStock;

        $limit = (int) $totalStockProd->qty - (int) $totalLastStock;

        try {
            if($limit > $qty){
                return true;
            }else{
                return false;
            }

        }catch(\Exception $e){
            return false;
        }

    }

    /**
     * Delete one user.
     *
     * @return Response
     */
    public function destroy(Request $request)
    {
        try {
            $prod = TrProduction::findOrFail($request->input('id'));

            $prod->delete();

            return response()->json(['message' => 'Successfully deleted'], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'prod not found!'], 404);
        }

    }

}