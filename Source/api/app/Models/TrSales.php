<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TrSales extends Model
{
    protected $table = 'trans_sales';

    public function item()
    {
        return $this->hasOne('App\Models\MItem', 'id' , 'item_id');
    }
}
