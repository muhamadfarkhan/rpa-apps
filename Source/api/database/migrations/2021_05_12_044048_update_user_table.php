<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class UpdateUserTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::table('users', function (Blueprint $table) {
            $table->integer('sup_id')->default(0)->after('email')->nullable();
            $table->integer('area_id')->default(0)->after('email')->nullable();
            $table->integer('rpa_id')->default(0)->after('email')->nullable();
            $table->integer('level')->default(0)->after('email')->nullable();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn('sup_id');
            $table->dropColumn('area_id');
            $table->dropColumn('rpa_id');
            $table->dropColumn('level');
        });
    }
}
