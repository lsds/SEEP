set terminal term 
expdir=sprintf("%s/%s/%sk/%s%s/%ss",outputdir,timestr,k,var,varext,session)
set output sprintf("%s/op_weight_info_fixed_kvarsession_%s%s",expdir,timestr,termext)

set xlabel "Tx time (mm:ss since the hour)"
set ylabel "Queue length (batches)"
set yrange [0:*]
set xtics font ",4"
set xdata time
set timefmt "%s"


set border linewidth 1.5
set style line 1 linewidth 2.5 linecolor rgb "red"
set style line 2 linewidth 2.5 linecolor rgb "blue"
set style line 3 linewidth 2.5 linecolor rgb "green"
set style line 4 linewidth 2.5 linecolor rgb "pink"
#set boxwidth 0.1
set style fill empty 
set offsets graph 0.0, 0.0, 0.1, 0.1
op_weight_info_files=system(sprintf("find %s -maxdepth 1 -name 'op_*_weight_infos.txt' | sed 's/.*op_\\([-]\\?[0-9]\\+\\)_weight_infos.txt/\\1/'", expdir))

#plot for [file in op_tput_files] file using 1:2 notitle w steps linestyle 1
set title sprintf("Operator total queue lengths \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
plot for [i=1:words(op_weight_info_files)] sprintf("%s/op_%s_weight_infos.txt", expdir, word(op_weight_info_files,i)) using 1:2 title word(op_weight_info_files,i) w fsteps linestyle i

set title sprintf("Operator input queue lengths \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
plot for [i=1:words(op_weight_info_files)] sprintf("%s/op_%s_weight_infos.txt", expdir, word(op_weight_info_files,i)) using 1:3 title word(op_weight_info_files,i) w fsteps linestyle i

set title sprintf("Operator output queue lengths \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
plot for [i=1:words(op_weight_info_files)] sprintf("%s/op_%s_weight_infos.txt", expdir, word(op_weight_info_files,i)) using 1:4 title word(op_weight_info_files,i) w fsteps linestyle i

#set title sprintf("Operator ready queue lengths \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
#plot for [i=1:words(op_weight_info_files)] sprintf("%s/op_%s_weight_infos.txt", expdir, word(op_weight_info_files,i)) using 1:5 title word(op_weight_info_files,i) w fsteps linestyle i

#set title sprintf("Operator weights \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
#plot for [i=1:words(op_weight_info_files)] sprintf("%s/op_%s_weight_infos.txt", expdir, word(op_weight_info_files,i)) using 1:6 title word(op_weight_info_files,i) w fsteps linestyle i

#set title sprintf("Pending 0 \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
#plot for [i=1:words(op_weight_info_files)] sprintf("%s/op_%s_weight_infos.txt", expdir, word(op_weight_info_files,i)) using 1:7 title word(op_weight_info_files,i) w fsteps linestyle i

#set title sprintf("Pending 1 \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
#plot for [i=1:words(op_weight_info_files)] sprintf("%s/op_%s_weight_infos.txt", expdir, word(op_weight_info_files,i)) using 1:8 title word(op_weight_info_files,i) w fsteps linestyle i
