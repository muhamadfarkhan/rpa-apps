<?php

use App\Helpers\MasterHelper;

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
    
    // Master User
    $router->get('profile', 'Master\UserController@profile');
    $router->get('users', 'Master\UserController@all');
    $router->get('users/{id}', 'Master\UserController@detail');
    $router->post('user/create', 'Master\UserController@store');
    $router->post('user/update', 'Master\UserController@update');
    $router->post('user/destroy', 'Master\UserController@destroy');
    $router->post('user/change_pwd', 'Master\UserController@changePwd');
    
    // Master RPA 
    $router->get('rpas', 'Master\RPAController@all');
    $router->get('rpa/{id}', 'Master\RPAController@detail');
    $router->post('rpa/create', 'Master\RPAController@store');
    $router->post('rpa/update', 'Master\RPAController@update');
    $router->post('rpa/destroy', 'Master\RPAController@destroy');

    // Master Area 
    $router->get('areas', 'Master\AreaController@all');
    $router->get('area/{id}', 'Master\AreaController@detail');
    $router->post('area/create', 'Master\AreaController@store');
    $router->post('area/update', 'Master\AreaController@update');
    $router->post('area/destroy', 'Master\AreaController@destroy');

    // Master Item 
    $router->get('items', 'Master\ItemController@all');
    $router->get('item/{id}', 'Master\ItemController@detail');
    $router->post('item/create', 'Master\ItemController@store');
    $router->post('item/update', 'Master\ItemController@update');
    $router->post('item/destroy', 'Master\ItemController@destroy');

    // List data
    $router->get('list/level', function(){
        return MasterHelper::listLevelUser();
    });

    // Transact Tonase (Header Detail)
    $router->get('tonase/header', 'Trans\TonaseHController@all');
    $router->get('tonase/header/{id}', 'Trans\TonaseHController@detail');
    $router->post('tonase/header/create', 'Trans\TonaseHController@store');
    $router->post('tonase/header/update', 'Trans\TonaseHController@update');
    $router->post('tonase/header/destroy', 'Trans\TonaseHController@destroy');

    $router->get('tonase/detail/{id}', 'Trans\TonaseDController@detail');
    $router->post('tonase/detail/create', 'Trans\TonaseDController@store');
    $router->post('tonase/detail/update', 'Trans\TonaseDController@update');
    $router->post('tonase/detail/destroy', 'Trans\TonaseDController@destroy');

});
 
