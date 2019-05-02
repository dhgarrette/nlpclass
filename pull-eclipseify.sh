for d in *; do

  if [ -d $d ]; then

	  echo $d

	  cd $d
	  if [ -e build.sbt ]; then

	  	git checkout -- .
	  	git pull

	  	sbt "eclipse with-source=true"

	    #mv build.sbt build.sbt.old
	    #sed "s/name := \".*\"/name := \"zz-$d\"/" build.sbt.old > build.sbt

	    mv .project .project.old
	    sed "s|<name>.*</name>|<name>zz-$d</name>|" .project.old > .project
	    rm .project.old

	  fi
	  cd ..

  fi

done
