# specify the base image
FROM python:3.12-bookworm

WORKDIR /app

ENV DEBIAN_FRONTEND=noninteractive
# Configure default locale (important for chrome-headless-shell). 
ENV LANG en_US.UTF-8
RUN apt-get -y update && apt-get install -y nodejs npm wget gnupg software-properties-common protobuf-compiler apt-transport-https

# setup temurin java
RUN wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null \
  && echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
  && apt update \
  && apt install -y temurin-11-jdk

# Install latest chrome dev package and fonts to support major charsets (Chinese, Japanese, Arabic, Hebrew, Thai and a few others)
# Note: this installs the necessary libs to make the bundled version of Chromium that Puppeteer
# installs, work.
RUN apt-get update \
  && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/googlechrome-linux-keyring.gpg \
  && sh -c 'echo "deb [arch=amd64 signed-by=/usr/share/keyrings/googlechrome-linux-keyring.gpg] https://dl-ssl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list' \
  && apt-get update \
  && apt-get install -y google-chrome-stable fonts-ipafont-gothic fonts-wqy-zenhei fonts-thai-tlwg fonts-khmeros fonts-kacst fonts-freefont-ttf libxss1 dbus dbus-x11 \
  --no-install-recommends \
  && rm -rf /var/lib/apt/lists/*

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

# set ant home
ENV ANT_HOME /usr/local/apache-ant-${ANT_VERSION}
ENV JAVA_HOME /usr/lib/jvm/temurin-11-jdk-amd64

WORKDIR /app
COPY . /app/
ENV ANT_OPTS="-Xmx256m -Dfile.encoding=UTF-8 -XX:+UnlockExperimentalVMOptions -XX:-UseG1GC -XX:+UseZGC"
RUN ant package

WORKDIR /app/example
RUN npm install

CMD ["/bin/bash"]
