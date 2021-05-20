<?php

namespace App\Http\Controllers\Trans;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\TrHTonase;
use App\Models\TrDTonase;

class TonaseDController extends Controller
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
         return response()->json(['tonase_detail' =>  TrHTonase::all()], 200);
    }

    /**
     * Get one MArea.
     *
     * @return Response
     */
    public function detail($id)
    {
        try {
            $tonase = TrDTonase::findOrFail($id);

            return response()->json(['tonase_detail' => $tonase], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'Tonase not found!'], 404);
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
            'kilogram' => 'required|regex:/^\d+(\.\d{1,2})?$/',
            'ekor' => 'required|integer',
            'tonase_id' => 'required|integer'
        ]);

        if($this->checkHeader((int) $request->input('tonase_id'))){

            try {

                $tonase = new TrDTonase;
                $tonase->kilogram = $request->input('kilogram');
                $tonase->ekor = $request->input('ekor');
                $tonase->tonase_id = (int) $request->input('tonase_id');
                
                $tonase->save();

                //return successful response
                return response()->json(['tonase' => $tonase, 'message' => 'Successfully created'], 201);

            } catch (\Exception $e) {
                //return error message
                return response()->json(['message' => 'Create detail tonase Failed!', 'error' => $e ], 409);
            }
        }else{
            return response()->json(['message' => 'Create detail tonase Failed!', 'error' => 'Data header not found' ], 409);
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

            $tonase = TrDTonase::find($request->input('id'));
            $tonase->kilogram = $request->input('kilogram');
            $tonase->ekor = $request->input('ekor');
            
            $tonase->save();

            //return successful response
            return response()->json(['tonase' => $tonase, 'message' => 'Successfully updated'], 201);

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
            $tonase = TrDTonase::findOrFail($request->input('id'));

            $tonase->delete();

            return response()->json(['message' => 'Successfully deleted'], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'tonase not found!'], 404);
        }

    }

}
