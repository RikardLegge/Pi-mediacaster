BASEDIR=$(dirname $0)
cd $BASEDIR

case $1 in
     "mkfifo") mkfifo datapipe;;
     "rmfifo") rm datapipe;;
     
     "omxplayer") omxplayer -o hdmi $2 < datapipe & echo -n . > datapipe;;
     "omxplayer_youtube") omxplayer -o hdmi "$(youtube-dl -s -g $2)" < datapipe & echo -n . > datapipe;;
     
     "omxplayer_quit") echo -n q > datapipe;;
     "omxplayer_pause") echo -n p > datapipe;;
     "omxplayer_seek-30") echo -n $'[D' > datapipe;;
     "omxplayer_seek30") echo -n $'[C' > datapipe;;
     "omxplayer_seek-600") echo -n $'[B' > datapipe;;
     "omxplayer_seek600") echo -n $'[A' > datapipe;;
     "omxplayer_info") echo -n z > datapipe;;
     
esac