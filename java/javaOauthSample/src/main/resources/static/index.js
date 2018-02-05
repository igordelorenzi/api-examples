(function () {

    var params = getHashParams();

    var oauthSource = document.getElementById('oauth-template').innerHTML,
        oauthTemplate = Handlebars.compile(oauthSource),
        oauthPlaceholder = document.getElementById('oauth');

    var productSource = document.getElementById('product-template').innerHTML,
        productTemplate = Handlebars.compile(productSource),
        productPlaceholder = document.getElementById('products');

    var access_token = params.access_token,
        refresh_token = params.refresh_token,
        error = params.error;

    var page = 0, size = 10;

    function initialize() {
        if (error) {
            alert('There was an error during the authentication');
            return;
        }

        if (access_token) {
            renderLoggedIn(access_token, refresh_token);
        } else {
            renderLoggedOut();
        }
        initializeComponents(refresh_token);

    }

    function initializeComponents(refresh_token) {
        document.getElementById('obtain-new-token').addEventListener('click', refreshToken, false);
    }

    function refreshToken(){
        $.ajax({
            url: '/user/refreshToken',
            data: {
                'refresh_token': refresh_token
            }
        }).done(function (data) {
            access_token = data.access_token;
            refresh_token = data.refresh_token;

            var oauthData = {
                access_token: access_token,
                refresh_token: refresh_token
            }

            oauthPlaceholder.innerHTML = oauthTemplate(oauthData);
            location.href = location.href.split("?")[0] + "?" + $.param(oauthData);
        })
        .error(logError);
    }

    function initializeProductComponents() {
        var buttonDelete = $('.delete-product');

        buttonDelete.click(function (el) {
            var confirmation = confirm("Are you sure ?");
            if (confirmation) {
                deleteProduct(el.target.getAttribute('data-id'), access_token);
            }
        });
    }

    function renderLoggedIn(access_token, refresh_token) {
        oauthPlaceholder.innerHTML = oauthTemplate({
            access_token: access_token,
            refresh_token: refresh_token
        });

        listProducts(page, size, access_token);

        $('#login').hide();
        $('#loggedin').show();
    };

    function renderLoggedOut() {
        $('#login').show();
        $('#loggedin').hide();
    }

    /**
     * Obtains parameters from the hash of the URL
     * @return Object
     */
    function getHashParams() {
        var hashParams = {};
        var e, r = /([^&;=]+)=?([^&;]*)/g,
            q = window.location.search.substring(1);
        while (e = r.exec(q)) {
            hashParams[e[1]] = decodeURIComponent(e[2]);
        }
        return hashParams;
    }

    /**
     * Call the Node.js server to list the products
     */
    function listProducts(page, size, access_token) {
        $.ajax({ url: '/user/products', data: { 'page': page, 'size': size, 'access_token': access_token } })
            .done(function (data) {
                productPlaceholder.innerHTML = productTemplate(data);
                initializeProductComponents();
            })
            .error(logError);
    }

    function deleteProduct(id, access_token) {
        $.ajax({ type: 'DELETE', url: '/user/deleteProduct?' + $.param({ 'id': id, 'access_token': access_token }) })
            .done(function (data) {
                page = 0;
                listProducts(page, size, access_token);
            })
            .error(logError);
    }

    function logError(data) {
        console.log(data.error)
    }

    initialize();
})();
