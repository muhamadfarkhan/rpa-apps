<?php

namespace App\Http\Controllers\Trans;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\TrHTonase;
use App\Models\TrProduction;

class ProductionController extends Controller
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
     * Get all Area.
     *
     * @return Response
     */
    public function all()
    {
         return response()->json(['productions' =>  TrProduction::all()], 200);
    }

    /**
     * Get one MArea.
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
                }
            }

            return response()->json(['tonase_header' => $tonase, 'productions' => $detail], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'Production not found! ' . $e, 'error' => $e], 404);
        }

    }

    /**
     * Store a new user.
     *
     * @param  Request  $request
     * @return Response
     */
    public function store(Request $request)
    {
        //validate incoming request 
        $this->validate($request, [
            'qty' => 'required|integer',
            'tonase_id' => 'required|integer',
            'user_id' => 'required|integer',
            'item_id' => 'required|integer',
            'capital_price' => 'required|integer',
            'sell_price' => 'required|integer',
            'processed_at' => 'required'
        ]);

        if($this->checkHeader((int) $request->input('tonase_id'))){

            try {

                $production = new TrProduction;
                $production->user_id = (int) $request->input('user_id');
                $production->item_id = (int) $request->input('item_id');
                $production->tonase_id = (int) $request->input('tonase_id');
                $production->capital_price = (int) $request->input('capital_price');
                $production->sell_price = (int) $request->input('sell_price');
                $production->processed_at = $request->input('processed_at');
                $production->qty = (int) $request->input('qty');
                
                $production->save();

                //return successful response
                return response()->json(['production' => $production, 'message' => 'Successfully created'], 201);

            } catch (\Exception $e) {
                //return error message
                return response()->json(['message' => 'Create detail production Failed!', 'error' => $e ], 409);
            }

        }else{
            return response()->json(['message' => 'Create detail production Failed!', 'error' => 'Data header not found' ], 409);
        }
    }

    private function checkHeader($id){
        try {
            $tonase = TrHTonase::findOrFail($id);

            return true;

        } catch (\Exception $e) {

            return false;
        }
    }

    /**
     * Change a existing user.
     *
     * @param  Request  $request
     * @return Response
     */
    public function update(Request $request)
    {
        //validate incoming request 
        $this->validate($request, [
            'id' => 'required|integer',
        ]);

        try {

            $production = TrProduction::find($request->input('id'));
            $production->user_id = (int) $request->input('user_id');
            $production->item_id = (int) $request->input('item_id');
            $production->tonase_id = (int) $request->input('tonase_id');
            $production->capital_price = (int) $request->input('capital_price');
            $production->sell_price = (int) $request->input('sell_price');
            $production->processed_at = $request->input('processed_at');
            $production->qty = (int) $request->input('qty');
            
            $production->save();

            //return successful response
            return response()->json(['production' => $production, 'message' => 'Successfully updated'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'Update detail tonase failed!', 'error' => $e], 409);
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