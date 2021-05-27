java -jar target/uberjar/gol2fca-0.1.0-SNAPSHOT-standalone.jar $1 $2
wait
cd latex
pdflatex "\newcommand{\lattices}{$2}\input{output}" > latex.log
wait
convert -alpha deactivate -verbose -delay 50 -loop 0 -density 300 output.pdf output.gif
mv output.gif ../gif/
cd ..
