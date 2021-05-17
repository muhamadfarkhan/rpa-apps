<?php

namespace App\Helpers;

use App\Models\User;

class MasterHelper{

    public static function listLevelUser()
    {
        $data['level'] = User::get();
        return $data;
    }

}