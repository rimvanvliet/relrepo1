#!/bin/bash

if [[ -z "$1" ]];
then
  releaseType="--minor";
else
  releaseType=$1;
fi

if ! [[ ${releaseType} == "--patch" || ${releaseType} == "--minor" || ${releaseType} == "--major" ]];
then
  echo Argument error:
  echo "Usage: $0  { --patch | (--minor) | --major }"
  exit 1
fi

# read the versionFiles from the project home directory
while read file; do files+=($file); done < ./versionFiles
if [[ ${#files[@]} == 0 ]]; then
  echo "Configuration error:"
  echo "versionFiles required in the home driectory of the project"
  exit 1
fi
rgx='version.*([0-9]+)\.([0-9]+)\.([0-9]+)'


# --------------------------------------------------------
# check if a branch is clean
# --------------------------------------------------------
checkClean() {
  if [[ -n $(git status -s; git status | grep ahead) ]];
  then
    echo '--------------------------------'
    echo ">>> $1 branch is NOT clean";
    echo '--------------------------------'
    git status
    exit 1
  fi
}

# --------------------------------------------------------
# check if all files contain the same version information
# --------------------------------------------------------
checkVersions () {
  unset refRelease;
  for file in "${files[@]}";
  do
      relstring=`egrep -m 1 $1 ${file}`
      # echo ">>>> ${relstring} in ${file}"
      if [[ ${relstring} =~ $1 ]];
      then
        major=${BASH_REMATCH[1]}; minor=${BASH_REMATCH[2]}; patch=${BASH_REMATCH[3]};
        if [[ ! -n $refRelease ]];
        then
          refRelease=${major}.${minor}.${patch};
        elif [[ $refRelease != ${major}.${minor}.${patch} ]];
        then
          echo '>>> Different versions: '${major}.${minor}.${patch}' in '${file}' does not equal '${refRelease};
          exit 1;
        fi
      else
        echo '>>> File '${file}' does not contain version information: '$1;
        exit 1;
      fi
  done
}

# -------------------------------------------------------
# check if versionnumbers correspond with the releaseType
# -------------------------------------------------------
checkReleaseType() {
  if [[ ${releaseType} == "--patch" ]]; then
    let refpatch=${mpatch}+1;
    if [[ ${major} != ${mmajor} || ${minor} != ${mminor} || ${patch} != ${refpatch} ]]; then
      echo ">>> Wrong patch versions: ${mmajor}.${mminor}.${mpatch} -> ${major}.${minor}.${patch}";
      exit 1
    fi
  elif [[ ${releaseType} == "--minor" ]]; then
    let refminor=${mminor}+1;
    if [[ ${major} != ${mmajor} || ${minor} != ${refminor} || ${patch} != 0 ]]; then
      echo ">>> Wrong minor versions: ${mmajor}.${mminor}.${mpatch} -> ${major}.${minor}.${patch}";
      exit 1
    fi
  elif [[ ${releaseType} == "--major" ]]; then
    let refmajor=${mmajor}+1;
    if [[ ${major} != ${refmajor} || ${minor} != 0 || ${patch} != 0 ]]; then
      echo ">>> Wrong major versions: ${mmajor}.${mminor}.${mpatch} -> ${major}.${minor}.${patch}";
      exit 1
    fi
  fi
}

# -------------------------------------------
# check if develop can be merged into master
# -------------------------------------------
checkMergeability() {
  tfilename=$(mktemp /tmp/patch.XXXXXXXXX)
  git checkout master
  git diff master develop > ${tfilename};
  if git apply --check ${tfilename};
  then
    echo "No merge conflict found."
    rm ${tfilename};
  else
    echo '>>> Merge conflicts ...'
    rm ${tfilename};
    exit 1
  fi
}

# -------------------------------------------
# set releaseVersion and nextVersion
# -------------------------------------------
# echo 'refRelease' $refRelease
setVersions() {
  releaseVersion=${major}.${minor}.${patch};
  if [[ ${releaseType} == "--patch" ]]; then
    let patch=${patch}+1
  elif [[ ${releaseType} == "--minor" ]]; then
    let minor=${minor}+1
  else # ${releaseType} == "--major"
    let major=${major}+1
  fi
  nextVersion=${major}.${minor}.${patch};
}

# -------------------------------------------
# remove .bak files
# -------------------------------------------
removeBakFiles() {
  for file in "${files[@]}"
  do
  rm -f ./${file}.bak
  done
}

# -----------------------------------------------
# end of subroutines, let's start the real stuff
# -----------------------------------------------

git checkout master
checkClean master
git pull
checkVersions ${rgx}

mmajor=${major};mminor=${minor};mpatch=${patch}

git checkout develop
checkClean develop
git pull
checkVersions ${rgx}'-SNAPSHOT'

checkReleaseType

checkMergeability
# -----------------------------------------------
# all checks OK
# -----------------------------------------------

setVersions

echo "-----------------------------------------";
echo ">>> master ${releaseVersion} will be released";
echo ">>> develop will be bumped to ${nextVersion}-SNAPSHOT";
echo
echo ">>> Continue? [y/N]"
read continue
if [[ ${continue} != "y" ]];
then
  echo "Coward!";
  exit 1
fi
echo "Let's go!";

git checkout master
if ! git merge develop; then # just to be shure ...
  git merge --abort
  exit 1
fi

for file in "${files[@]}"
do
sed -i.bak "1,/${releaseVersion}-SNAPSHOT/ s/${releaseVersion}-SNAPSHOT/${releaseVersion}/" ./${file}
done

removeBakFiles

git add -A
git commit -m"Bumped version to ${releaseVersion}"
git push
git tag -a ${releaseVersion} -m "Release ${releaseVersion}."
git push --tag

git checkout develop
git merge master

for file in "${files[@]}"
do
  sed -i.bak "1,/${releaseVersion}/ s/${releaseVersion}/${nextVersion}-SNAPSHOT/" ./${file}
done

removeBakFiles

git add -A
git commit -m"Bumped version to ${nextVersion}-SNAPSHOT"
git push
