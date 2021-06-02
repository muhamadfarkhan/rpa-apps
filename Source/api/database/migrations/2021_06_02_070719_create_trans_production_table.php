<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateTransProductionTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('trans_production', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->bigInteger('tonase_id');
            $table->bigInteger('user_id');
            $table->date('processed_at');
            $table->bigInteger('item_id');
            $table->bigInteger('qty');
            $table->decimal('capital_price',8,2)->default(0.00);
            $table->decimal('sell_price',8,2)->default(0.00);
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
        Schema::dropIfExists('trans_production');
    }
}
