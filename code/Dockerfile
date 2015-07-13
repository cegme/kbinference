# Docker file for datapath

# NOTE:
# Build y using the following command: docker build -t cegme/kbinference .
# Run a bash instance using the following command: docker run -t -i cegme/kbinference
# Log into postgres: psql -d madlib -U postgres
# Password 'mypass'

FROM ubuntu
MAINTAINER christangrant@gmail.com

# Standard upgrade
RUN apt-get -y update &&\
  apt-get -y upgrade &&\
  apt-get -y dist-upgrade

RUN apt-get -y install groovy maven git wget curl htop vim emacs python scala software-properties-common 

RUN apt-get -y install libboost-all-dev libboost-dev libboost-dbg libopenblas-dev liblapack-dev libarpack2-dev libarpack2-dbg libarpack+++2-dev libarmadillo-dev libeigen2-dev g++ gcc sqlite3 libkrb5-dev



# Need to edit files to get the new postgresql
RUN touch /etc/apt/sources.list.d/pgdg.list &&\
    echo "deb http://apt.postgresql.org/pub/repos/apt/ wheezy-pgdg main" >> etc/apt/sources.list.d/pgdg.list  &&\
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add - &&\
    sudo apt-get update &&\
    sudo apt-get dist-upgrade
	

RUN apt-get -y install postgresql-client-9.4 postgresql-contrib-9.4 postgresql-server-dev-9.4 libpq-dev postgresql-plpython-9.4 python-software-properties postgresql-hll postgresql-9.4-dbg build-essential postgresql-9.4-dbg postgresql-9.4-pgmp postgresql-contrib-9.4 postgresql-9.4-preprepare

RUN mkdir /data/ && chmod 777 /data

# Create the database
USER postgres

RUN /etc/init.d/postgresql start &&\
  createdb -T template0 -O postgres cubismfacts &&\
  createuser -s -w -e cgrant &&\
  mkdir -p /data/ && cd data &&\
  wget http://www.cise.ufl.edu/~cgrant/data/cubismfacts.sql.gz &&\
  gunzip -c cubismfacts.sql.gz | psql -U postgres cubismfacts

# EXTREMELY unsafe hack for easy DB access
RUN sed -i 's/peer/trust/' /etc/postgresql/9.4/main/pg_hba.conf
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/9.4/main/pg_hba.conf

RUN echo "listen_addresses='*'" >> /etc/postgresql/9.4/main/postgresql.conf

USER root

RUN mkdir -p /home/dsr/ && cd /home/dsr &&\
  chmod 777 /home/dsr/ &&\
  git clone https://github.com/cegme/kbinference.git &&\
  cd /home/dsr/kbinference/code/src/test/resources/ &&\
  wget -O - http://www.cise.ufl.edu/~cgrant/data/reverb_clueweb_tuples-1.1.triples.clean.csv.gz | gunzip -c > reverb_clueweb_tuples-1.1.triples.clean.csv

#USER postgres
 
#RUN /etc/init.d/postgresql start &&\
#  psql --command "ALTER USER postgres WITH SUPERUSER PASSWORD 'mypass';" 

USER root

CMD /etc/init.d/postgresql start && cd /home/dsr/ && /bin/bash



