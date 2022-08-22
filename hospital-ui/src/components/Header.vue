<template>
  <div>
    <h1>Tanzu Hospital</h1>
    <p>{{welcomeMessage}}</p>
  </div>
</template>

<script>
export default {
  data() {
    return {
      welcomeMessage: ''
    }
  },
  mounted: function () {
    fetch('http://hospital-api.frontdoor.tanzuhospital.com/api/welcomeMessage', {method: 'GET'})
        .then(response => {
          return response.text();
        })
        .then(data => {
          this.welcomeMessage = data;
        });
  }
}
</script>
