<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class DelColumnTotalHeaderTonaseTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::table('trans_header_tonase', function (Blueprint $table) {
            $table->decimal('price',8,2)->default(0.00)->after('user_id');
        });

        Schema::table('trans_header_tonase', function (Blueprint $table) {
            $table->dropColumn('tonase');
            $table->dropColumn('total_ekor');
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
            $table->decimal('tonase',8,2);
            $table->integer('total_ekor')->default(0);
        });

        Schema::table('trans_header_tonase', function (Blueprint $table) {
            $table->dropColumn('price');
        });
    }
}
