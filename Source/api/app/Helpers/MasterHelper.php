<?php

namespace App\Helpers;

use App\Models\MGeneralCode;

class MasterHelper{

    public static function listLevelUser()
    {
        $data['level'] = MGeneralCode::where('header','level_user')->get();
        return $data;
    }

}