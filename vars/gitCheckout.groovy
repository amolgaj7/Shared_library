def call(String repoUrl, String branch = 'master'){
git branch: branch, url: repoUrl
}
