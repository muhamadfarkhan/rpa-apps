package com.rpathechicken.api

class ApiEndPoint {
    companion object{

        private const val BASE = "http://api-rpa.farkhan.net/api"
        //private const val BASE = "http://192.168.1.7:8000/api"
        //private const val BASE = "http://10.161.159.75:8000/api"

        const val login = "$BASE/login"
        const val change_pwd = "$BASE/user/change_pwd"
        const val profile = "$BASE/profile"
        const val list_level = "$BASE/list/level"
        const val list_rpa = "$BASE/list/rpa"
        const val list_area = "$BASE/list/area"
        const val user_list = "$BASE/users"
        const val user_detail = "$BASE/user"
        const val user_create = "$BASE/user/create"
        const val user_update = "$BASE/user/update"
        const val user_destroy = "$BASE/user/destroy"
        const val rpa_list = "$BASE/rpas"
        const val rpa_detail = "$BASE/rpa"
        const val rpa_create = "$BASE/rpa/create"
        const val rpa_update = "$BASE/rpa/update"
        const val rpa_destroy = "$BASE/rpa/destroy"
        const val area_list = "$BASE/areas"
        const val area_detail = "$BASE/area"
        const val area_create = "$BASE/area/create"
        const val area_update = "$BASE/area/update"
        const val area_destroy = "$BASE/area/destroy"
        const val item_list = "$BASE/items"
        const val item_detail = "$BASE/item"
        const val item_create = "$BASE/item/create"
        const val item_update = "$BASE/item/update"
        const val item_destroy = "$BASE/item/destroy"

        //transaction
        const val tonase_header_list = "$BASE/tonase/header"
        const val tonase_header_detail = "$BASE/tonase/header"
        const val tonase_header_create = "$BASE/tonase/header/create"
        const val tonase_header_update = "$BASE/tonase/header/update"
        const val tonase_header_destroy = "$BASE/tonase/header/destroy"

        const val tonase_detail_create = "$BASE/tonase/detail/create"
        const val tonase_detail_update = "$BASE/tonase/detail/update"
        const val tonase_detail_destroy = "$BASE/tonase/detail/destroy"

    }
}