<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TrStock extends Model
{
    protected $table = 'trans_stock';

    public function item()
    {
        return $this->hasOne('App\Models\MItem', 'id' , 'item_id');
    }

    public function seller()
    {
        return $this->hasOne('App\Models\User', 'id' , 'seller_id');
    }

    public function area()
    {
        return $this->hasOne('App\Models\MArea', 'id' , 'area_id');
    }
}
