<?php

/** @var \Laravel\Lumen\Routing\Router $router */

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It is a breeze. Simply tell Lumen the URIs it should respond to
| and give it the Closure to call when that URI is requested.
|
*/

$router->get('/', function () use ($router) {
    return $router->app->version();
});

// API route group
$router->group(['prefix' => 'api'], function () use ($router) {
    
    $router->post('register', 'AuthController@register');
    
    $router->post('login', 'AuthController@login');
    
    $router->get('profile', 'Master\UserController@profile');

    $router->get('users/{id}', 'Master\UserController@singleUser');

    $router->get('users', 'Master\UserController@allUsers');
    
    $router->post('user/create', 'Master\UserController@store');

    $router->post('user/update', 'Master\UserController@update');

    $router->post('user/destroy', 'Master\UserController@destroy');
    
});
 
