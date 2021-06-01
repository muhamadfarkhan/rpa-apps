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
         return response()->json(['tonase_headers' =>  TrHTonase::orderBy('processed_at','desc')
         ->all()], 200);
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

            $tonaseDetail = TrHTonase::findOrFail($id)->detail;

            $tonase['sum_ekor'] = $tonaseDetail->sum('ekor');
            $tonase['sum_kilo'] = $tonaseDetail->sum('kilogram');

            return response()->json(['tonase_header' => $tonase, 'tonase_detail' => $tonaseDetail], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'Tonase not found!', 'error' => $e], 404);
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
            'rpa_id' => 'required|integer'
        ]);

        try {

            $tonase = new TrHTonase;
            $tonase->plat_number = $request->input('plat_number');
            $tonase->processed_at = date('Y-m-d');
            $tonase->rpa_id = (int) $request->input('rpa_id');
            $tonase->tonase = 0.00;
            $tonase->user_id = Auth::user()->id;
            
            $tonase->save();

            //return successful response
            return response()->json(['tonase' => $tonase, 'message' => 'Successfully created'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'Create header tonase Failed!', 'error' => $e], 409);
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
