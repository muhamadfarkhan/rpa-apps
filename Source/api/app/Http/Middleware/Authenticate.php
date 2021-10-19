<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Contracts\Auth\Factory as Auth;
use App\Models\UserToken;
use DB;

class Authenticate
{
    /**
     * The authentication guard factory instance.
     *
     * @var \Illuminate\Contracts\Auth\Factory
     */
    protected $auth;

    /**
     * Create a new middleware instance.
     *
     * @param  \Illuminate\Contracts\Auth\Factory  $auth
     * @return void
     */
    public function __construct(Auth $auth)
    {
        $this->auth = $auth;
    }

    /**
     * Handle an incoming request.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \Closure  $next
     * @param  string|null  $guard
     * @return mixed
     */
    public function handle($request, Closure $next, $guard = null)
    {
        if ($this->auth->guard($guard)->guest()) {
            // return response('Unauthorized.', 401);
            return response()->json(['message' => 'You are Unauthorized. Please login first!'], 401);
        }

        if($this->checkToken($request->header('Authorization'),$request)){
            return $next($request);
        }else{
            return response()->json(['message' => 'You are Unauthorized. Please login first!'], 401);
        }
    }

    private function checkToken($token,$request){
        try {
            $user = UserToken::where('token',$token)->firstOrFail();
            return true;

        } catch (\Exception $e) {
            return false;
        }
    }
}
