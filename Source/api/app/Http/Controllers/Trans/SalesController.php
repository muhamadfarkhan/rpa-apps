<?php

namespace App\Http\Controllers\Trans;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\TrHTonase;
use App\Models\TrProduction;
use App\Models\TrStock;
use App\Models\TrSales;
use App\Models\MItem;

class SalesController extends Controller
{
     /**
     * Instantiate a new areaController instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('auth');
    }

    /**
     * Get all Item Stock.
     *
     * @return Response
     */
    public function all()
    {
        switch (Auth::user()->level) {
            case 1:
              return $this->salesAdmin();
              break;
            case 2:
              return $this->salesRPA();
              break;
            case 3:
              return $this->salesArea();
              break;
            default:
              return $this->salesSeller();
        } 
    }

    /**
     * Get detail salesAdmin.
     *
     * @return Response
     */
    public function salesAdmin()
    {
        $tonase = TrHTonase::orderBy('processed_at','desc')->get();

        foreach($tonase as $row){
            if($row->production->count()){
                $data[] = $row;
                $row->production = $row->production();
                $row->price = number_format($row->price);
                $row->rpa = $row->rpa;
            }
        }
        
        return response()->json(['sales' => $data ], 200);

    }

    /**
     * Get detail sales Seller.
     *
     * @return Response
     */
    private function salesSeller()
    {
        $today = date('Y-m-d');

        $stock = TrHTonase::where('processed_at',$today)->first()->stock;

        $detail = array();
        foreach($stock as $row){
            $detail[] = $row;
            $row->qty = $row->qty.' unit';
            $row->item_name = (!empty($row->item)) ? $row->item->name : '';
        }
        
      return response()->json(['stock' => $detail, "you" => Auth::user()], 200);

    }

}