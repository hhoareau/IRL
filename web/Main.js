DOMAIN="http://localhost:8080";FACEBOOK_ID="600859120078292";
//DOMAIN="http://162.168.0.11:8080";FACEBOOK_ID="605906262906911";
//DOMAIN="https://opt-adopt.appspot.com";FACEBOOK_ID="597602987070572";
ROOT_API=DOMAIN+'/_ah/api';

var email="";

function loadScript(url, callback){   // Adding the script tag to the head as suggested before
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;
    script.onreadystatechange = callback;
    script.onload = callback;
    head.appendChild(script);
}


function init(){
    gapi.client.load('irl', 'v1', function(){
        console.log("gapi loaded. Start call");
        start();
    }, ROOT_API);
}


function sendPosition(email,lat,lng,func){
    gapi.client.irl.position({email:email,lat:lat,lng:lng}).then(func);
}

function getobjects(idgame,email,distance,func){
    gapi.client.irl.getobjects({idgame:idgame,email:email,distance:distance}).then(func);
}

function quit(email,idgame,func){
    gapi.client.irl.quit({idgame:idgame,email:email}).then(func);
}

function raz(func){
    gapi.client.irl.raz().then(func);
}

function callsbymin(func){
    gapi.client.irl.callsbymin().then(func);
}

function refreshGame(idgame){
    gapi.client.irl.refreshgame({idgame:idgame}).then(function(res){});
}

function createGameFromKML(flags,teams,email,kml,func){
    gapi.client.irl.createfromkml({flags:flags,email:email,teams:teams,kml:kml}).then(func);
}

function createGameFromPts(flags,teams,email,pts,func){
    gapi.client.irl.createfrompts({flags:flags,email:email,teams:teams,pts:pts}).then(func);
}

function createCaptureTheFlag(teams,email,pts,func){
    gapi.client.irl.createcapturetheflag({email:email,teams:teams,pts:pts}).then(func);
}


function getGame(idgame,func){
    gapi.client.irl.getgame({idgame:idgame}).then(function(res){
        func(res);
    });
}


function addBotToGame(name,func){
    gapi.client.irl.adduser({info:name,UID:"",domain:DOMAIN,bot:true}).then(func);
}

function adduser(response,func){
    gapi.client.irl.adduser({info:JSON.stringify(response),UID:"",domain:DOMAIN,bot:false}).then(func);
}

function join(email,idgame,func){
    gapi.client.irl.join({email:email,idgame:idgame}).then(func);
}


function utf8_to_b64(str) {
    return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function(match, p1) {
        return String.fromCharCode('0x' + p1);
    }));
}

function sendmap(map,idgame,func){

    //var code=utf8_to_b64(map);

    var req= gapi.client.request({
            path: ROOT_API+'/irl/v1/sendmap',
            method: 'POST',
            params: {'idgame':idgame},
            headers: {
                'X-Upload-Content-Type': '*/*',
                'X-Upload-Content-Length': map.length,
                //'Content-Type': 'Content-Type: application/xml; charset="utf-8"'
                'Content-Type': 'Content-Type: application/json; charset="utf-8"'
            },
            body:{"xml":map}
    });
    req.execute(func);
}


function httpGetAsync(theUrl, callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText);
    };
    xmlHttp.open("GET", theUrl, true); // true for asynchronous
    xmlHttp.send(null);
}


function sendPhoto(frm){
    gapi.client.irl.send({})
}

function getallusers(func){
    gapi.client.irl.allusers().then(func);
}

function getParam(param) {
    var vars = {};
    window.location.href.replace( location.hash, '' ).replace(
        /[?&]+([^=&]+)=?([^&]*)?/gi, // regexp
        function( m, key, value ) { // callback
            vars[key] = value !== undefined ? value : '';
        }
    );
    return vars;
}


function showGames(elt,email,distance){
    rc="";
    gapi.client.irl.getgames({email:email,distance:distance}).then(function(games){
        if(games.result.items.length==0)
            rc="aucune partie dispo";
        else
            games.result.items.forEach(function(game){
                rc=rc+"<br><a href='wait.html?email="+email+"&idgame="+game.id+"'>"+game.name+"</a>";
            });
        elt.innerHTML=rc;
    });
}



function showObjects(elt){
    rc="";
    gapi.client.irl.getallobjects().then(function(flags){
        if(flags.result.items.length==0)
            rc="aucun objets dispo";
        else
            flags.result.items.forEach(function(f){
                rc=rc+"<br>"+JSON.stringify(f);
            });
        elt.innerHTML=rc;
    });
}


function showPlayer(elt){
    var rc="";
    gapi.client.irl.players().then(function(players){
        players.result.items.forEach(function(player){
            rc=rc+"<br><a href='main.html?id="+player.id+"'>"+player.name+"</a>";
        });
        elt.innerHTML=rc;
    });
}


function showUsers(elt){
    var rc="";
    gapi.client.irl.allplayers().then(function(players){
        players.result.items.forEach(function(user){
            rc=rc+"<br>"+user.email;
        });
        elt.innerHTML=rc;
    });
}


loadScript("https://apis.google.com/js/client.js?onload=init",function(){
//loadScript(DOMAIN+"/client.js?onload=init",function(){
    console.log("Chargement de tous les scripts ok");
});
