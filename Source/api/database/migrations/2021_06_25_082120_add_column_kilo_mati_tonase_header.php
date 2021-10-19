<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class AddColumnKiloMatiTonaseHeader extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::table('trans_header_tonase', function (Blueprint $table) {
            $table->decimal('kilo_mati',8,2)->default(0.00)->after('total_mati')->nullable();
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
            $table->dropColumn('kilo_mati');
        });
    }
}
