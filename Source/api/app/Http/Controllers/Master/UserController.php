<?php

namespace App\Http\Controllers\Master;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\User;
use App\Models\MRpa;
use App\Models\MGeneralCode;
use App\Models\MArea;
use Illuminate\Support\Facades\Hash;

class UserController extends Controller
{
     /**
     * Instantiate a new UserController instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('auth');
    }

    /**
     * Get the authenticated User.
     *
     * @return Response
     */
    public function profile()
    {
        return response()->json(['user' => Auth::user()], 200);
    }

    /**
     * Get all User.
     *
     * @return Response
     */
    public function all()
    {
         return response()->json(['users' =>  User::all()], 200);
    }

    /**
     * Get one user.
     *
     * @return Response
     */
    public function detail($id)
    {
        try {
            $user = User::findOrFail($id);

            $rpa = MRpa::where('id',$user->rpa_id)->first();
            $level = MGeneralCode::where('header','level_user')->where('code',$user->level)->first();
            $area = MArea::where('id',$user->area_id)->first();

            return response()->json(['user' => $user, 'rpa' => $rpa, 'level' => $level, 'area' => $area], 200);

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
            'email' => 'required|email|unique:users',
            'password' => 'required|confirmed',
        ]);

        try {

            $user = new User;
            $user->name = $request->input('name');
            $user->username = str_replace(' ','',$request->input('username'));
            $user->email = $request->input('email');
            $user->phone = $request->input('phone');
            $user->level = $request->input('level');
            $user->rpa_id = $request->input('rpa_id');
            $user->area_id = $request->input('area_id');
            $user->sup_id = $request->input('sup_id');
            $plainPassword = $request->input('password');
            $user->password = app('hash')->make($plainPassword);

            $user->save();

            //return successful response
            return response()->json(['user' => $user, 'message' => 'Successfully created'], 201);

        } catch (\Exception $e) {
            //return error message
            return response()->json(['message' => 'User Registration Failed!' . $e], 409);
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

            $user = User::find($request->input('id'));
            $user->name = $request->input('name');
            $user->phone = $request->input('phone');
            $user->level = $request->input('level');
            $user->rpa_id = $request->input('rpa_id');
            $user->area_id = $request->input('area_id');
            $user->sup_id = $request->input('sup_id');

            $user->save();

            //return successful response
            return response()->json(['user' => $user, 'message' => 'Successfully updated'], 201);

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
            $user = User::findOrFail($request->input('id'));

            $user->delete();

            return response()->json(['message' => 'Successfully deleted'], 200);

        } catch (\Exception $e) {

            return response()->json(['message' => 'User not found!'], 404);
        }

    }

    /**
     * Change password user.
     *
     * @return Response
     */
    public function changePwd(Request $request)
    {
        // return Auth::user()->id;

        $old_password = $request->old_password;
        $new_password = $request->new_password;

        $current_password = Auth::User()->password;

        if(Hash::check($old_password, $current_password)){
           
            $data['password'] = app('hash')->make($request->new_password);

            try {
                User::where('id', Auth::user()->id)
                        ->update($data);
                        
                return response()->json(['result' => true, 'message' =>  'Yeay... Password berhasil diubah'], 200);

            } catch (\Exception $e) {
                //return error message
                return response()->json(['message' => 'Change password failed!', 'error' => $e], 409);
            }

        }else{
            return response()->json(['message' =>  'Oops... Salah memasukan password anda saat ini'], 409);
        }

        

    }

}
