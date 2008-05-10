<!--
  Petclinic :: a Spring Framework demonstration
-->

<html>
  <head>
    <title>Petclinic - A Compass Sample Application</title>
      <link rel=stylesheet href="styles/style.css">
        <script>
            function setCurrentLink()
            {
                var l = document.links;
                for (var i = 0; i < l.length; i++) {
                    var a = l[i].href;
                    var b = top.location.href;
                    a = a.replace("file:///", "file:/");
                    b = b.replace("file:///", "file:/");
                    if (a == b) {
                        l[i].className = "current";
                        break;
                    }
                }
            }
        </script>
  </head>

  <body onload="setCurrentLink();">
        <!--begin sidebar -->
        <%@ include file="/WEB-INF/jsp/sidebar.jsp" %>
        <!--end sidebar -->

        <!--main body-->

        <div id="content">
