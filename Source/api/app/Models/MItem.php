<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class MItem extends Model
{
    protected $table = 'm_item';

    public function getNameAttribute($value)
    {
        return ucfirst($value);
    }

    public function production()
    {
        return $this->hasMany('App\Models\TrProduction', 'item_id' , 'id');
    }
}
