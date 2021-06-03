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

    public function production()
    {
        return $this->hasMany('App\Models\TrProduction', 'tonase_id' , 'id');
    }

    public function rpa()
    {
        return $this->hasOne('App\Models\MRpa', 'id' , 'rpa_id');
    }
}
