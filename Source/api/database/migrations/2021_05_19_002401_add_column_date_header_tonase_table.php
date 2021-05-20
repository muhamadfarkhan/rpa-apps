<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class AddColumnDateHeaderTonaseTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::table('trans_header_tonase', function (Blueprint $table) {
            $table->date('processed_at')->after('plat_number');
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::table('trans_header_tonase', function (Blueprint $table) {
            $table->dropColumn('processed_date');
        });
    }
}
