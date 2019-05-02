# NLP Class


## Updating website

FIRST, commit changes to the website source in `support`.

THEN, in the repository root:

    git checkout support
    cd site
    jekyll build
    cd ..
    git checkout gh-pages
    cp -rf site/_site/* .
    git add .
    git commit . -m "updated site"
    git push
    git checkout support


## Development

Start the server:

    jekyll --server
    jekyll --server --auto  # recompiles html files after changes

Browse to:

    http://0.0.0.0:4000






