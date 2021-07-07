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
}
