set terminal term 
expdir=sprintf("%s/%s/%sk/%s%s/%ss",outputdir,timestr,k,var,varext,session)
set output sprintf("%s/link-tputs/link_tput_fixed_kvarsession_%s%s",expdir,timestr,termext)

set title sprintf("Link interval tputs \nk=%s, %s=%s, query=%s, duration=%s, session=%s",k,varname,var,query,duration,session)
set xlabel "Tx time (Seconds since epoch)"
set ylabel "Tput"
set y2label "ETX"
set yrange [0:1000]
set y2range [0:12]
set xtics font ",4"
set ytics nomirror
set y2tics
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
link_tput_files=system(sprintf("find %s/link-tputs -maxdepth 1 -name 'op_*_up_*_link_interval_tput.txt' | sed 's/.*op_\\([-]\\?[0-9]\\+\\)_up_\\([-]\\?[0-9]\\+\\)_link_interval_tput.txt/\\1_up_\\2/'", expdir))

#plot for [file in op_tput_files] file using 1:2 notitle w steps linestyle 1
#plot for [i=1:words(link_tput_files)] sprintf("%s/link-tputs/op_%s_link_interval_tput.txt", expdir, word(link_tput_files,i)) using 1:2 title word(link_tput_files,i) w fsteps linestyle i
# costs
do for [i=1:words(link_tput_files)]{
plot sprintf("%s/link-tputs/op_%s_link_interval_tput.txt", expdir, word(link_tput_files,i)) using 1:2 title word(link_tput_files,i) w fsteps linestyle 1, \
sprintf("%s/link-tputs/op_%s_link_interval_tput.txt", expdir, word(link_tput_files,i)) using 1:4 title word(link_tput_files,i) axes x1y2 w fsteps linestyle 2 
}


# costs
#plot for [i=1:1] sprintf("%s/link-tputs/op_%s_link_interval_tput.txt", expdir, word(link_tput_files,i)) using 1:4 title word(link_tput_files,i) w fsteps linestyle i
