<?php

namespace App\Helpers;

use App\Models\MGeneralCode;
use App\Models\MArea;
use App\Models\MRpa;

class MasterHelper{

    public static function listLevelUser()
    {
        $data['levels'] = MGeneralCode::where('header','level_user')->get();
        return $data;
    }

    public static function listRPA()
    {
        $data['rpas'] = MRpa::get();
        return $data;
    }

    public static function listArea()
    {
        $data['areas'] = MArea::get();
        return $data;
    }

}