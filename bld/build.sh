#!/bin/bash
export JAVA_HOME="/usr/java/latest"
echo "Starting build..."
VERSION=$(cat ./ver/version.txt)
echo "Version: $VERSION"
BUILDID=$(xmllint --xpath "//buildenvironment/build/id" buildenv.xml | sed -re 's/<\/?\w+>//g')
echo "Build ID: $BUILDID"
FULLVERSION="$VERSION-build$BUILDID"
echo "Full version: $FULLVERSION"

echo
echo "#----------------------------#"
echo "# PREPARE ARTIFACT DIRECTORY #"
echo "#----------------------------#"
SOURCEDIR="$(pwd)/src"
SOURCEARTIFACT="$SOURCEDIR/library/artifacts"
ARTIFACTNAME="com.reginald.swiperefresh.library"
ARTIFACTROOTDIR="$(pwd)/_artifact"
ARTIFACTDIR="$ARTIFACTROOTDIR/$ARTIFACTNAME/content"
echo "Artifact dir: $ARTIFACTDIR"
mkdir -p $ARTIFACTDIR

# LOG DIR??

pushd .
cd $SOURCEDIR

# echo
# echo "#--------------------#"
# echo "# Preparatons        #"
# echo "#--------------------#"


# echo "count=0" > /home/ec/.android/repositories.cfg
# Download and install tools
# echo y | $ANDROID_HOME/tools/android update sdk --no-ui --all --filter tools,platform-tools,build-tools-23.0.3,android-24,extra-android-support,extra-android-m2repository,extra-google-m2repository

echo
echo "#--------------------#"
echo "# Build              #"
echo "#--------------------#"
./gradlew build -PmyVersion="$VERSION" -PmyBuild="$BUILDID"

if [ $? -ne 0 ]
then
    echo "Build failed!"
    exit 1
fi

echo
echo "#--------------------#"
echo "# Testing            #"
echo "#--------------------#"
./gradlew test -PmyVersion="$VERSION" -PmyBuild="$BUILDID"

if [ $? -ne 0 ]
then
    echo "Test failed!"
    exti 2
fi

echo
echo "#--------------------#"
echo "# Publishing         #"
echo "#--------------------#"
./gradlew publish -PmyVersion="$VERSION" -PmyBuild="$BUILDID"

if [ $? -ne 0 ]
then
    echo "Publishing failed!"
    exit 3
fi

#Copy to artifact folder
cp -R $SOURCEARTIFACT/* $ARTIFACTDIR

popd

#Writing artifact.xml
cat > "$ARTIFACTROOTDIR/$ARTIFACTNAME/artifact.xml" <<EOF
<artifact>
    <name>${ARTIFACTNAME}</name>
    <version>${FULLVERSION}</version>
    <workflow>
      <project>Deploy</project>
      <name>SimpleDeploy.Android.PrereleaseRelease</name>
    </workflow>
  </artifact>
EOF

#Copying deploy.ps1
cp "./deploy/deploy.ps1" $ARTIFACTROOTDIR/$ARTIFACTNAME
