var request = require('request'); // "Request" library

module.exports = {

  list : function(req,res) {

    var options = {
      url: 'https://api.contaazul.com/v1/customers?page='+ req.query.page + '&size=' + req.query.size,
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

  },

  create: function () {
    var options = {
      url: 'https://api.contaazul.com/v1/customers',
      headers: { 'Authorization': 'Bearer ' + req.query.access_token },
      formData: parameters
    };

    request.post(options, function (error, response, body) {
      if (!error && response.statusCode === 204) {
        res.send(body);
      } else {
        res.send({ 'error' : error });
      }
    });
  },

  delete : function(req,res) {

    var options = {
      url: 'https://api.contaazul.com/v1/customers/'+ req.query.id,
      headers: { 'Authorization': 'Bearer ' + req.query.access_token },
      json: true
    };

    request.del(options, function(error, response) {
      if (!error && response.statusCode === 204) {
        res.send('ok');
      } else {
        res.send({ 'error' : error });
      }
    });
  }

};