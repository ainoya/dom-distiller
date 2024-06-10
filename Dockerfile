# specify the base image
FROM python:3.12-bookworm

WORKDIR /app

ENV DEBIAN_FRONTEND=noninteractive
# Configure default locale (important for chrome-headless-shell). 
ENV LANG en_US.UTF-8
RUN apt-get update && apt-get install -y nodejs npm wget gnupg software-properties-common protobuf-compiler apt-transport-https chromium

# setup temurin java
RUN mkdir -p /etc/apt/keyrings \
  && wget -O - wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc \
  && echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
  && apt-get update \
  && apt-get -y install temurin-8-jdk

# install ant
# ant version
ENV ANT_VERSION 1.10.13
# download ant
WORKDIR /tmp
RUN wget -q https://downloads.apache.org/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz

# extract ant
RUN tar -xzf apache-ant-${ANT_VERSION}-bin.tar.gz -C /usr/local

# create a symbolic link
RUN ln -s /usr/local/apache-ant-${ANT_VERSION}/bin/ant /usr/bin/ant

RUN pip install --upgrade pip
RUN pip install protobuf

RUN  groupadd -r pptruser && useradd -rm -g pptruser -G audio,video pptruser

# set ant home
ENV ANT_HOME /usr/local/apache-ant-${ANT_VERSION}
ENV PUPPETEER_EXECUTABLE_PATH /usr/bin/chromium
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD true

WORKDIR /app
COPY . /app/
RUN ant extractjs

# chown
RUN chown -R pptruser:pptruser /app
USER pptruser

WORKDIR /app/example
RUN npm install

CMD ["/bin/bash"]
