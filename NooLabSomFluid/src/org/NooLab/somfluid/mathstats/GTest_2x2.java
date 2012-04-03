package org.NooLab.somfluid.mathstats;

public class GTest_2x2 {

}
/*


  with value_StrGrid_obj do
  begin
    with intermediates^ do
    begin
      if col_row_headers then offs:=1 else offs:=0 ;

           val(cells[offs,offs],hr1,j) ;
           if hr1=0 then
           begin
             cell_freqs[1] := 0
           end  
             else
      cell_freqs[1] := hr1*ln(hr1) ;
           val(cells[offs,offs+1],hr1,j) ;
           if hr1=0 then
             cell_freqs[2] := 0
             else
      cell_freqs[2] := hr1*ln(hr1) ;
           val(cells[offs+1,offs],hr1,j) ;
           if hr1=0 then
             cell_freqs[3] := 0
             else
      cell_freqs[3] := hr1*ln(hr1) ;
           val(cells[offs+1,offs+1],hr1,j) ;
           if hr1=0 then
             cell_freqs[4] := 0
             else
      cell_freqs[4] := hr1*ln(hr1) ;



                 hr2 :=0 ;
           hr1 := rc_sum(rows[offs],offs) ; hr2:=hr2+hr1 ;
           if hr1=0 then
           begin
             significance:='less data' ; exit ;
           end;
      marg_freqs[1] := hr1*ln(hr1) ;
           hr1 := rc_sum(rows[offs+1],offs) ;  hr2:=hr2+hr1 ;
           if hr1=0 then
           begin
             significance:='less data' ; exit ;
           end;
      marg_freqs[2] := hr1*ln(hr1) ;
           hr1 := rc_sum(cols[offs],offs) ;
      marg_freqs[3] := hr1*ln(hr1) ;
           hr1 := rc_sum(cols[offs+1],offs) ;
      marg_freqs[4] := hr1*ln(hr1) ;

      tfreq := rc_sum(rows[offs],offs)+rc_sum(rows[offs+1],offs) ;

      marg_freqs_sum :=0 ;
      cell_freqs_sum :=0 ;

      for i:=1 to 4 do
      begin
        marg_freqs_sum := marg_freqs_sum + marg_freqs[i] ;
        cell_freqs_sum := cell_freqs_sum + cell_freqs[i];
      end;


      total_freqs := hr2*ln(hr2) ;

      G_stat := 2 * ( cell_freqs_sum - marg_freqs_sum + total_freqs ) ;


     { calculating Williams'-correction}
             zt1:=0 ;
             zt2:=0 ;

             zt1 :=   (tfreq/(val_str( cells[offs,offs] )+val_str( cells[offs+1,offs] ))) +(tfreq/(val_str( cells[offs,offs+1] )+val_str( cells[offs+1,offs+1] ))) - 1 ;
             zt2 :=   (tfreq/(val_str( cells[offs,offs] )+val_str( cells[offs,offs+1] ))) +(tfreq/(val_str( cells[offs+1,offs] )+val_str( cells[offs+1,offs+1] ))) - 1 ;


        q := 1 + ((zt1)*(zt2)/(6*tfreq)) ;

      G_stat := G_stat/q ;

    end ;

  end; { with }

      { getting the alpha-value for d.f.= 1  and  G_stat
        file "CHIALPHA.TAB" in directory of exe }

   _res := score_at_critical_chi2_value( G_stat,1,
                                         crit_val, sig_level_str ,1) ;

      { smaller than the critical value => n.s.}
    if write_to_target then
      write_to_target_grid( target_StrGrid_obj ,
                            G_stat, crit_val, 1,
                            'G(adj)',sig_level_str ) ;

     significance := pchar(sig_level_str) ;


*/