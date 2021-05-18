<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateTransDetailTonaseTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('trans_detail_tonase', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->integer('tonase_id');
            $table->decimal('kilogram',8,2);
            $table->integer('ekor')->default(0);
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
        Schema::dropIfExists('trans_detail_tonase');
    }
}
