<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TrHTonase extends Model
{
    protected $table = 'trans_header_tonase';

    public function detail()
    {
        return $this->hasMany('App\Models\TrDTonase', 'tonase_id' , 'id');
    }
}
