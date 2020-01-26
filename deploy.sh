# Script to deploy server to Heroku
git checkout Heroku_deployment
git pull origin master

rm -rf android_app
rm -rf desktop_app
rmdir desktop_app

rm -f .travis.yml
rm -f README.md
rm -f deploy.sh


git add .
git commit -m "Deploy server to Heroku"
git push