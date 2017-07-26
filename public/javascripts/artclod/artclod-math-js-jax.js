ARTC.addLoadEvent(
    function(){
        var elements = document.querySelectorAll('[data-mathjs2jax]');
        var elementsMathJs  = _.map(elements, function(e){ return e.getAttribute('data-mathjs2jax'); });

        for (var i = 0; i < elements.length; i++) {
            var mathTex = math.parse(elementsMathJs[i]).toTex();
            var mathJax = ARTC.mathJax.tex(mathTex);
            ARTC.mathJax.updateByElem(elements[i], mathJax);
        }
    }
);