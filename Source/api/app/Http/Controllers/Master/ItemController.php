<?php

namespace App\Http\Controllers\Master;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\MItem;

class ItemController extends Controller
{
     /**
     * Instantiate a new ItemController instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('auth');
    }

    /**
     * Get all Item.
     *
     * @return Response
     */
    public function all()
    {
         return response()->json(['items' =>  MItem::all()], 200);
    }

    /**
     * Get one MItem.
     *
     * @return Response
     */
    public function detail($id)
    {
        try {
            $item = MItem::findOrFail($id);

            return response()->json(['item' => $item], 200);

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
            'description' => 'required|string|',
        ]);

        try {

            $item = new MItem;
            $item->name = $request->input('name');
            $item->description = $request->input('description');
            
            $item->save();

            //return successful response
            return response()->json(['item' => $item, 'message' => 'Successfully created'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'Create item Failed!','error' => $e], 409);
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

            $item = MItem::find($request->input('id'));
            $item->name = $request->input('name');
            $item->description = $request->input('description');

            $item->save();

            //return successful response
            return response()->json(['item' => $item, 'message' => 'Successfully updated'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'Update item failed!', 'error' => $e], 409);
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
            $item = MItem::findOrFail($request->input('id'));

            $item->delete();

            return response()->json(['message' => 'Successfully deleted'], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'Item not found!'], 404);
        }

    }

}
