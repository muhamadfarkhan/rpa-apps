<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateTransSalesTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('trans_sales', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->bigInteger('tonase_id');
            $table->bigInteger('area_id');
            $table->bigInteger('seller_id');
            $table->bigInteger('item_id');
            $table->bigInteger('qty');
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
        Schema::dropIfExists('trans_sales');
    }
}
