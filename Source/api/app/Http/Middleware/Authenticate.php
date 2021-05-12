<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Contracts\Auth\Factory as Auth;

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
            return response('Unauthorized.', 401);
        }

        return $next($request);

        // if($this->checkToken($request->header('Authorization'),$request)){
        //     return $next($request);
        // }else{
        //     return response('You are Unauthorized. Please login.', 401);
        // }
    }

    private function checkToken($token,$request){
        try {
            $user = UserToken::where(DB::raw('CONVERT(VARCHAR(max), token)'),$token)->firstOrFail();

            return true;

        } catch (\Exception $e) {

            return false;
        }
    }
}
