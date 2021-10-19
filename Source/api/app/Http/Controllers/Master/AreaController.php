<?php

namespace App\Http\Controllers\Master;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\MArea;

class AreaController extends Controller
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
         return response()->json(
             ['areas' =>  MArea::select('m_area.*','m_rpa.name as rpa_name')
                        ->join('m_rpa','m_rpa.id','m_area.rpa_id')
                        ->orderBy('m_area.id','desc')->get() 
        
        ], 200);
    }

    /**
     * Get one MArea.
     *
     * @return Response
     */
    public function detail($id)
    {
        try {
            $area = MArea::select('m_area.*','m_rpa.name as rpa_name','m_rpa.id as rpa_id'
            ,'m_rpa.address as rpa_address')
            ->join('m_rpa','m_rpa.id','m_area.rpa_id')->findOrFail($id);

            return response()->json(['area' => $area], 200);

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
            'address' => 'required|string',
            'rpa_id' => 'required|integer',
        ]);

        try {

            $area = new MArea;
            $area->name = $request->input('name');
            $area->address = $request->input('address');
            $area->rpa_id = $request->input('rpa_id');
            
            $area->save();

            //return successful response
            return response()->json(['area' => $area, 'message' => 'Successfully created'], 201);

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

            $area = MArea::find($request->input('id'));
            $area->name = $request->input('name');
            $area->address = $request->input('address');
            $area->rpa_id = $request->input('rpa_id');

            $area->save();

            //return successful response
            return response()->json(['area' => $area, 'message' => 'Successfully updated'], 201);

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
            $area = MArea::findOrFail($request->input('id'));

            $area->delete();

            return response()->json(['message' => 'Successfully deleted'], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'area not found!'], 404);
        }

    }

}
