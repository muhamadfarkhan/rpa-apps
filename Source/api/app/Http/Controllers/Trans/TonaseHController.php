<?php

namespace App\Http\Controllers\Trans;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\TrHTonase;
use App\Models\TrDTonase;

class TonaseHController extends Controller
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
        $tonase = TrHTonase::orderBy('processed_at','desc')->get();

        foreach($tonase as $row){
            if(!empty($row->rpa)){
                $data[] = $row;
                $row->price = number_format($row->price);
                $row->rpa_name = $row->rpa->name;
            }
        }
        
        return response()->json(['tonase_headers' => $data ], 200);
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
            if(!empty(TrHTonase::findOrFail($id)->detail)){
                $tonaseDetail = TrHTonase::findOrFail($id)->detail;

                $tonase['sum_ekor'] = $tonaseDetail->sum('ekor');
                $tonase['sum_kilo'] = $tonaseDetail->sum('kilogram');
                $tonase['rpa_name'] = $tonase->rpa->name;
                $tonase['rpa_address'] = $tonase->rpa->address;
                
                foreach($tonaseDetail as $row){
                    $detail[] = $row;
                    $row->kilogram = $row->kilogram.' Kg';
                    $row->ekor = $row->ekor.' Ekor';
                }
            }

            return response()->json(['tonase_header' => $tonase, 'tonase_details' => $detail], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'Tonase not found! ' . $e, 'error' => $e], 404);
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
            'plat_number' => 'required|string',
            'rpa_id' => 'required|integer',
            'price' => 'required|regex:/^\d+(\.\d{1,2})?$/',
            'tonase_date' => 'required',
            'kilo_mati' => 'regex:/^\d+(\.\d{1,2})?$/',
        ]);

        if($this->checkDuplicateTOnase($request->input('tonase_date'),$request->input('rpa_id'))){

            try {

                $tonase = new TrHTonase;
                $tonase->plat_number = $request->input('plat_number');
                $tonase->processed_at = $request->input('tonase_date');//date('Y-m-d');
                $tonase->rpa_id = (int) $request->input('rpa_id');
                $tonase->price = (int) $request->input('price');
                $tonase->total_mati = (int) $request->input('total_mati');
                $tonase->kilo_mati = $request->input('kilo_mati');
                $tonase->user_id = Auth::user()->id;
                
                $tonase->save();

                //return successful response
                return response()->json(['tonase' => $tonase, 'message' => 'Successfully created'], 201);

            } catch (\Exception $e) {
                //return error message
                return response()->json(['message' => 'Create header tonase Failed! '. $e, 'error' => $e], 409);
            }

        }else{
            return response()->json(['message' => 'Tonase tersebut telah diproses. Mohon check kembali.'], 409);
        }

    }

    private function checkDuplicateTOnase($date,$rpa){
        $check = TrHTonase::where('processed_at',$date)->where('rpa_id',$rpa)->first();

        if(empty($check)){
            return true;
        }else{
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

            $tonase = TrHTonase::find($request->input('id'));
            $tonase->plat_number = $request->input('plat_number');
            
            $tonase->save();

            //return successful response
            return response()->json(['tonase' => $tonase, 'message' => 'Successfully updated'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'Update header tonase failed!', 'error' => $e], 409);
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
            $tonase = TrHTonase::findOrFail($request->input('id'));

            $tonaseDetail = TrDTonase::where('tonase_id',$request->input('id'));

            $tonaseDetail->delete();
            $tonase->delete();

            return response()->json(['message' => 'Successfully deleted'], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'tonase not found!'], 404);
        }

    }

}
