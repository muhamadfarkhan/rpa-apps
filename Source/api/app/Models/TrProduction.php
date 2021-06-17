<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TrProduction extends Model
{
    protected $table = 'trans_production';

    public function item()
    {
        return $this->hasOne('App\Models\MItem', 'id' , 'item_id');
    }
}
