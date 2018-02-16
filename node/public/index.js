(function() {

  var params = getHashParams();

  var oauthSource = document.getElementById('oauth-template').innerHTML,
      oauthTemplate = Handlebars.compile(oauthSource),
      oauthPlaceholder = document.getElementById('oauth');

  var salesSource = document.getElementById('sales-template').innerHTML,
      salesTemplate = Handlebars.compile(salesSource),
      salesPlaceholder = document.getElementById('sales');

  var invoicesSource = document.getElementById('invoices-template').innerHTML,
      invoicesTemplate = Handlebars.compile(invoicesSource),
      invoicesPlaceholder = document.getElementById('invoices');

  var productSource = document.getElementById('product-template').innerHTML,
      productTemplate = Handlebars.compile(productSource),
      productPlaceholder = document.getElementById('products');

  var access_token = params.access_token,
      refresh_token = params.refresh_token,
      error = params.error;

  var page=0, size=10;

  function initialize(){
    if (error) {
      alert('There was an error during the authentication');
      return;
    } else {
      var code = getParameterByName('code'),
          state = getParameterByName('state');
      if (code && state) {
        window.location = '/callback?code='+code+'&state='+state;
      } else {
        if (access_token) {
          renderLoggedIn(access_token, refresh_token);
        } else {
          renderLoggedOut();
        }
        initializeComponents( refresh_token);
      }
    }
  }

  function initializeComponents(refresh_token){
    document.getElementById('obtain-new-token').addEventListener('click', function() {
      $.ajax({
        url: '/refresh_token',
        data: {
          'refresh_token': refresh_token
        }
      }).done(function(data) {
        if( data.error ){
          console.log('Error '+data.error);
          console.log('Error desc. '+data.error_description);
          renderLoggedOut();
        }else {
          access_token = data.access_token;
          refresh_token = data.refresh_token;
          oauthPlaceholder.innerHTML = oauthTemplate({
            access_token: access_token,
            refresh_token: refresh_token
          });
        }
      });
    }, false);
    document.getElementById('logout').addEventListener('click', function() {
      renderLoggedOut();
    }, false);
  }

  function initializeSalesComponents(){
    document.getElementById('list-next').addEventListener('click', function() {
      page++;
      listSales(page, size, access_token);
    }, false);

    document.getElementById('list-previous').addEventListener('click', function() {
      if( page !== 0) {
        page--;
      }
      listSales(page, size, access_token);
    }, false);

    var buttonDelete = document.getElementById('delete-sale');
    buttonDelete.addEventListener('click', function() {
      var confirmation = confirm("Are you sure ?");
      if( confirmation ){
        deleteSale( buttonDelete.getAttribute('data-id'), access_token);
      }
    }, false);
  }

  function initializeInvoicesComponents(){
    document.getElementById('list-next').addEventListener('click', function() {
      page++;
      listInvoices(page, size, access_token);
    }, false);

    document.getElementById('list-previous').addEventListener('click', function() {
      if( page !== 0) {
        page--;
      }
      listInvoices(page, size, access_token);
    }, false);

    var buttonDelete = document.getElementById('delete-invoice');
    buttonDelete.addEventListener('click', function() {
      var confirmation = confirm("Are you sure ?");
      if( confirmation ){
        deleteInvoice( buttonDelete.getAttribute('data-id'), access_token);
      }
    }, false);
  }

  function initializeProductComponents(){
    document.getElementById('list-next').addEventListener('click', function() {
      page++;
      listProducts(page, size, access_token);
    }, false);

    document.getElementById('list-previous').addEventListener('click', function() {
      if( page !== 0) {
        page--;
      }
      listProducts(page, size, access_token);
    }, false);

    var buttonDelete = document.getElementById('delete-product');
    buttonDelete.addEventListener('click', function() {
      var confirmation = confirm("Are you sure ?");
      if( confirmation ){
        deleteProduct( buttonDelete.getAttribute('data-id'), access_token);
      }
    }, false);
  }

  function renderLoggedIn( access_token, refresh_token){
    // render oauth info
    oauthPlaceholder.innerHTML = oauthTemplate({
      access_token: access_token,
      refresh_token: refresh_token
    });

    listSales( page, size, access_token );
    // listInvoices( page, size, access_token );
    // listProducts( page, size, access_token );

    $('#login').hide();
    $('#loggedin').show();
  };

  function renderLoggedOut(){
    // render initial screen
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
        q = window.location.hash.substring(1);
    while ( e = r.exec(q)) {
       hashParams[e[1]] = decodeURIComponent(e[2]);
    }
    return hashParams;
  }

  function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
  }

  /**
   * Sales CRUD
   */

  function listSales(page,size,access_token){
    $.ajax({ url: '/list_sales', data: { 'page': page, 'size' : size, 'access_token': access_token }})
    .done(function(data) {
      if( data.error ){
        console.log('Error '+data.error);
        console.log('Error desc. '+data.error_description);
        renderLoggedOut();
      }else {
        salesPlaceholder.innerHTML = salesTemplate( data );
        initializeSalesComponents();
      }
    });
  }

  /**
   * Invoice CRUD
   */

  function listInvoices() {
    $.ajax({ url: '/list_invoices', data: { 'page': page, 'size' : size, 'access_token': access_token }})
    .done(function(data) {
      if( data.error ){
        console.log('Error '+data.error);
        console.log('Error desc. '+data.error_description);
        renderLoggedOut();
      }else {
        invoicesPlaceholder.innerHTML = invoicesTemplate( data );
        initializeInvoicesComponents();
      }
    });
  }

  /**
   * Call the Node.js server to list the products
   */
  function listProducts(page,size,access_token){
    $.ajax({ url: '/list_products', data: { 'page': page, 'size' : size, 'access_token': access_token }})
    .done(function(data) {
      if( data.error ){
        console.log('Error '+data.error);
        console.log('Error desc. '+data.error_description);
        renderLoggedOut();
      }else {
        productPlaceholder.innerHTML = productTemplate( data );
        initializeProductComponents();
      }
    });
  }

  function deleteProduct( id, access_token){
    $.ajax({ url: '/delete_product', data: { 'id': id, 'access_token': access_token }})
    .done(function(data) {
      if( data.error ){
        console.log('Error '+data.error);
        console.log('Error desc. '+data.error_description);
        renderLoggedOut();
      }else {
        page = 0;
        listProducts( page, size, access_token );
      }
    });
  }

  initialize();
})();
