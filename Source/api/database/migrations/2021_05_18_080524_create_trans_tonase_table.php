<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateTransTonaseTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('trans_header_tonase', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->integer('rpa_id');
            $table->integer('user_id');
            $table->decimal('tonase',8,2);
            $table->integer('total_ekor')->default(0);
            $table->integer('total_mati')->default(0);
            // $table->decimal('cap_price',8,2)->default(0.00);
            // $table->decimal('sell_price',8,2)->default(0.00);
            $table->string('plat_number')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('trans_tonase');
    }
}
