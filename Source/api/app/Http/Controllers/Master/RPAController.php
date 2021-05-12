<?php

namespace App\Http\Controllers\Master;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\MRpa;

class RPAController extends Controller
{
     /**
     * Instantiate a new RPAController instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('auth');
    }

    /**
     * Get all RPA.
     *
     * @return Response
     */
    public function all()
    {
         return response()->json(['rpas' =>  MRpa::all()], 200);
    }

    /**
     * Get one MRpa.
     *
     * @return Response
     */
    public function detail($id)
    {
        try {
            $rpa = MRpa::findOrFail($id);

            return response()->json(['rpa' => $rpa], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'user not found!'], 404);
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
            'name' => 'required|string',
            'address' => 'required|string|',
        ]);

        try {

            $rpa = new MRpa;
            $rpa->name = $request->input('name');
            $rpa->address = $request->input('address');
            
            $rpa->save();

            //return successful response
            return response()->json(['rpa' => $rpa, 'message' => 'Successfully created'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'User Registration Failed!'], 409);
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

            $rpa = MRpa::find($request->input('id'));
            $rpa->name = $request->input('name');
            $rpa->address = $request->input('address');;

            $rpa->save();

            //return successful response
            return response()->json(['rpa' => $rpa, 'message' => 'Successfully updated'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'Update user failed!', 'error' => $e], 409);
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
            $rpa = MRpa::findOrFail($request->input('id'));

            $rpa->delete();

            return response()->json(['message' => 'Successfully deleted'], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'RPA not found!'], 404);
        }

    }

}
