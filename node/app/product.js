var request = require('request'); // "Request" library

module.exports = {

  list : function(req,res) {

    var options = {
      url: 'https://api.contaazul.com/v1/products?page='+ req.query.page + '&size=' + req.query.size,
      headers: { 'Authorization': 'Bearer ' + req.query.access_token },
      json: true
    };

    request.get(options, function(error, response, body) {
      if (!error && response.statusCode === 200) {
        res.send(body);
      } else {
        res.send({ 'error' : error });
      }
    });

  }

};
